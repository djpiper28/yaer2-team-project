#pragma once
#include <list>
#include <string>

template <class T>
class FilteredList
{
public:
    FilteredList(); // For deferred construction
    FilteredList(std::list<T> base);
    void setBase(std::list<T> base);
    void filter(std::string query);
    std::list<T> getFiltered() const;
    size_t size() const;
private:
    std::list<T> original;
    std::list<T> filtered;
    std::string lastSearch;
};

template <class T>
FilteredList<T>::FilteredList()
{
    // Do nothing
}

template <class T>
FilteredList<T>::FilteredList(std::list<T> base)
{
    this->original = base;
    this->filtered = base;
}


template <class T>
void FilteredList<T>::filter(std::string query)
{
    if (query == "") {
        this->filtered = this->original;
        return;
    }

    this->filtered = std::list<T>();

    for (T item : this->original) {
        if (item.matches(query)) {
            this->filtered.push_back(item);
        }
    }

    this->lastSearch = query;
}

template <class T>
void FilteredList<T>::setBase(std::list<T> base)
{
    this->original = base;
    this->filter(this->lastSearch);
}

template <class T>
size_t FilteredList<T>::size() const
{
    return this->filtered.size();
}

template <class T>
std::list<T> FilteredList<T>::getFiltered() const
{
    return this->filtered;
}
